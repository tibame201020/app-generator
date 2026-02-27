import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Check, X, Mail } from 'lucide-react';
import { ProjectRole } from '../stores/useAuthStore';

interface ProjectInvitation {
    id: string;
    project: {
        id: string;
        name: string;
    };
    inviter: {
        username: string;
        email: string;
    };
    role: ProjectRole;
    status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
    createdAt: string;
}

export const UserInvitations: React.FC = () => {
    const [invitations, setInvitations] = useState<ProjectInvitation[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchInvitations = async () => {
        setLoading(true);
        try {
            const response = await axios.get('/api/invitations/me');
            setInvitations(response.data);
        } catch (e) {
            console.error("Failed to fetch invitations", e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchInvitations();
    }, []);

    const handleAccept = async (id: string) => {
        try {
            await axios.post(`/api/invitations/${id}/accept`);
            fetchInvitations();
            // Optionally trigger a project list refresh in parent
            window.location.reload(); // Simple way to refresh projects list
        } catch (e) {
            alert("Failed to accept invitation");
        }
    };

    const handleReject = async (id: string) => {
        if (!confirm("Reject this invitation?")) return;
        try {
            await axios.post(`/api/invitations/${id}/reject`);
            fetchInvitations();
        } catch (e) {
            alert("Failed to reject invitation");
        }
    };

    if (loading) return null;
    if (invitations.length === 0) return null;

    return (
        <div className="mb-8">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2 text-white">
                <Mail size={20} className="text-blue-400" />
                Pending Invitations
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {invitations.map(inv => (
                    <div key={inv.id} className="bg-gray-800 border border-blue-500/30 rounded-lg p-4 shadow-lg shadow-blue-900/10">
                        <div className="flex justify-between items-start mb-2">
                            <h3 className="font-semibold text-white text-lg">{inv.project.name}</h3>
                            <span className={`px-2 py-0.5 rounded text-xs font-mono border ${
                                inv.role === ProjectRole.ADMIN ? 'bg-purple-900/30 text-purple-300 border-purple-800' :
                                'bg-blue-900/30 text-blue-300 border-blue-800'
                            }`}>
                                {inv.role}
                            </span>
                        </div>
                        <p className="text-gray-400 text-sm mb-4">
                            Invited by <span className="text-white">{inv.inviter.username}</span>
                        </p>
                        <div className="flex gap-2">
                            <button
                                onClick={() => handleAccept(inv.id)}
                                className="flex-1 bg-blue-600 hover:bg-blue-500 text-white py-1.5 rounded text-sm font-medium flex items-center justify-center gap-1 transition-colors"
                            >
                                <Check size={16} /> Accept
                            </button>
                            <button
                                onClick={() => handleReject(inv.id)}
                                className="flex-1 bg-gray-700 hover:bg-gray-600 text-white py-1.5 rounded text-sm font-medium flex items-center justify-center gap-1 transition-colors"
                            >
                                <X size={16} /> Reject
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};
