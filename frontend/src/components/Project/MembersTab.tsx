import React, { useEffect, useState } from 'react';
import { ProjectRole } from '../../stores/useAuthStore';
import { useProjectStore } from '../../stores/useProjectStore';
import axios from 'axios';
import { Trash2, UserPlus, Shield } from 'lucide-react';

interface Member {
    id: string;
    user: {
        id: string;
        username: string;
        email: string;
    };
    role: ProjectRole;
    joinedAt: string;
}

interface MembersTabProps {
    projectId: string;
}

export const MembersTab: React.FC<MembersTabProps> = ({ projectId }) => {
    const [members, setMembers] = useState<Member[]>([]);
    const [loading, setLoading] = useState(false);
    const [newMemberEmail, setNewMemberEmail] = useState('');
    const [newMemberRole, setNewMemberRole] = useState<ProjectRole>(ProjectRole.VIEWER);
    const [error, setError] = useState('');

    const { canManageMembers } = useProjectStore();
    const canManage = canManageMembers();

    const fetchMembers = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`/api/projects/${projectId}/members`);
            setMembers(response.data);
        } catch (e) {
            console.error("Failed to fetch members", e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMembers();
    }, [projectId]);

    const handleAddMember = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        try {
            await axios.post(`/api/projects/${projectId}/members`, {
                email: newMemberEmail,
                role: newMemberRole
            });
            setNewMemberEmail('');
            fetchMembers();
        } catch (err: any) {
            setError(err.response?.data || "Failed to add member");
        }
    };

    const handleUpdateRole = async (userId: string, role: ProjectRole) => {
        try {
            await axios.put(`/api/projects/${projectId}/members/${userId}`, { role });
            fetchMembers();
        } catch (e) {
            alert("Failed to update role");
        }
    };

    const handleRemoveMember = async (userId: string) => {
        if (!confirm("Are you sure you want to remove this member?")) return;
        try {
            await axios.delete(`/api/projects/${projectId}/members/${userId}`);
            fetchMembers();
        } catch (e) {
            alert("Failed to remove member");
        }
    };

    return (
        <div className="p-4 bg-gray-900 text-white h-full overflow-y-auto">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
                <Shield size={20} />
                Project Members
            </h2>

            {canManage && (
                <form onSubmit={handleAddMember} className="mb-6 bg-gray-800 p-4 rounded border border-gray-700">
                    <h3 className="text-sm font-semibold mb-2 text-gray-300">Add Member</h3>
                    <div className="flex gap-2">
                        <input
                            type="email"
                            placeholder="User Email"
                            value={newMemberEmail}
                            onChange={e => setNewMemberEmail(e.target.value)}
                            className="flex-1 bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-blue-500"
                            required
                        />
                        <select
                            value={newMemberRole}
                            onChange={e => setNewMemberRole(e.target.value as ProjectRole)}
                            className="bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-blue-500"
                        >
                            <option value={ProjectRole.VIEWER}>Viewer</option>
                            <option value={ProjectRole.MEMBER}>Member</option>
                            <option value={ProjectRole.ADMIN}>Admin</option>
                        </select>
                        <button
                            type="submit"
                            className="bg-blue-600 hover:bg-blue-500 text-white px-4 py-1.5 rounded text-sm font-medium flex items-center gap-1 transition-colors"
                        >
                            <UserPlus size={16} /> Add
                        </button>
                    </div>
                    {error && <div className="text-red-400 text-xs mt-2">{error}</div>}
                </form>
            )}

            <div className="bg-gray-800 rounded border border-gray-700 overflow-hidden">
                <table className="w-full text-sm text-left">
                    <thead className="bg-gray-750 text-gray-400 font-medium border-b border-gray-700">
                        <tr>
                            <th className="px-4 py-2">User</th>
                            <th className="px-4 py-2">Role</th>
                            <th className="px-4 py-2">Joined</th>
                            {canManage && <th className="px-4 py-2 text-right">Actions</th>}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {loading ? (
                            <tr><td colSpan={4} className="px-4 py-4 text-center text-gray-500">Loading...</td></tr>
                        ) : members.length === 0 ? (
                            <tr><td colSpan={4} className="px-4 py-4 text-center text-gray-500">No members found</td></tr>
                        ) : (
                            members.map(member => (
                                <tr key={member.id} className="hover:bg-gray-750/50">
                                    <td className="px-4 py-2">
                                        <div className="font-medium text-white">{member.user.username}</div>
                                        <div className="text-gray-500 text-xs">{member.user.email}</div>
                                    </td>
                                    <td className="px-4 py-2">
                                        {canManage ? (
                                            <select
                                                value={member.role}
                                                onChange={(e) => handleUpdateRole(member.user.id, e.target.value as ProjectRole)}
                                                className="bg-gray-700 border-none rounded px-2 py-1 text-xs outline-none cursor-pointer hover:bg-gray-600"
                                            >
                                                <option value={ProjectRole.VIEWER}>Viewer</option>
                                                <option value={ProjectRole.MEMBER}>Member</option>
                                                <option value={ProjectRole.ADMIN}>Admin</option>
                                            </select>
                                        ) : (
                                            <span className={`px-2 py-0.5 rounded text-xs font-mono border ${
                                                member.role === ProjectRole.ADMIN ? 'bg-purple-900/30 text-purple-300 border-purple-800' :
                                                member.role === ProjectRole.MEMBER ? 'bg-blue-900/30 text-blue-300 border-blue-800' :
                                                'bg-gray-700 text-gray-300 border-gray-600'
                                            }`}>
                                                {member.role}
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-4 py-2 text-gray-400 text-xs">
                                        {new Date(member.joinedAt).toLocaleDateString()}
                                    </td>
                                    {canManage && (
                                        <td className="px-4 py-2 text-right">
                                            <button
                                                onClick={() => handleRemoveMember(member.user.id)}
                                                className="text-red-500 hover:text-red-400 p-1 rounded hover:bg-gray-700 transition-colors"
                                                title="Remove Member"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </td>
                                    )}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
